#!/usr/bin/env python
import argparse
import signal
import subprocess
import sys
import time
import queue
import threading

PATH_TO_BINARY = './target/universal/stage/bin/fruitarian'
FIRST_PORT = 5000

procs = []


def log(str):
    """Log str immediately to stdout"""
    sys.stdout.write(str)
    sys.stdout.flush()


def enqueue_output(out, idx, queue):
    """
    Enqueue some output in order to be written in order later on.

    out: output (i.e. of stdout of a process)
    idx: internal index of the node/process
    queue: the queue to enqueue to
    """
    for line in iter(out.readline, b''):
        queue.put(f"[{idx}] | {line}")
    out.close()


def package():
    """Runs `sbt clean stage` and waits for it to finish."""
    log("-- Running sbt clean stage\n")
    proc = subprocess.Popen(['sbt', 'clean', 'stage'], stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, universal_newlines=True)
    for line in proc.stdout:
        log(f" | {line}")
    proc.wait()


def start_first_node():
    """Start the first node of the fruitarian network without any parameters."""
    log("-- Starting fruitarian node 0\n")
    proc = subprocess.Popen(['./target/universal/stage/bin/fruitarian'], stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, universal_newlines=True)
    procs.append(proc)


def add_node(idx, server_port, known_port):
    """
    Add a node to the fruitarian network.

    idx: the current node index (only for reference within this script and logs)
    server_port: the port at which to start the server at this node
    known_port: the port of an already known node
    """
    log(f"-- Starting fruitarian node {idx}\n")
    proc = subprocess.Popen(['./target/universal/stage/bin/fruitarian', str(server_port), 'localhost', str(known_port)],
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
    procs.append(proc)


def parse_args():
    """Parse the arguments of the script"""
    parser = argparse.ArgumentParser(
        description="Run the Fruitarian project")

    parser.add_argument('-n', '--nodes', type=int, required=True,
                        help="number of nodes to start")
    parser.add_argument('-s', '--skip-packaging', action='store_true',
                        help="skip packaging of the Scala project")
    return parser.parse_args()


def stop(sig, frame):
    """Gracefully stop all the processes"""
    print("-- Stopping...")
    for proc in procs:
        proc.terminate()
    sys.exit(0)


def main():
    args = parse_args()
    # Register handler for SIGINT
    signal.signal(signal.SIGINT, stop)

    # Package the project using sbt
    if not args.skip_packaging:
        package()
    if args.nodes < 1:
        sys.exit("At least one node should be started")

    # Start a first node and add any additional nodes
    start_first_node()
    for i in range(1, args.nodes):
        # Make sure the nodes have some time to start
        time.sleep(1)
        add_node(i, FIRST_PORT + i, FIRST_PORT + i - 1)

    # Start threads and a queue for queueing the outputs of the processes/nodes
    threads = []
    writequeue = queue.Queue()
    for idx, p in enumerate(procs):
        threads.append(threading.Thread(target=enqueue_output,
                                        args=(p.stdout, idx, writequeue)))
    for t in threads:
        t.daemon = True
        t.start()

    # Continuously check the queue for any new output and log it
    while True:
        try:
            line = writequeue.get_nowait()
        except queue.Empty:
            # Prevent Python process from going haywire
            time.sleep(.01)
            pass
        else:
            log(line)

        if all(proc.poll() is not None for proc in procs):
            break

    log("All nodes stopped")
    sys.exit(0)


if __name__ == '__main__':
    main()
