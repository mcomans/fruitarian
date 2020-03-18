#!/usr/bin/env python
import argparse
import signal
import subprocess
import sys
import time

PATH_TO_BINARY = './target/universal/stage/bin/fruitarian'
FIRST_PORT = 5000

logfile = open('runner_output.txt', 'w')
procs = []


def log(str):
    sys.stdout.write(str)
    logfile.write(str)


def package():
    log("-- Running sbt clean stage\n")
    proc = subprocess.Popen(['sbt', 'clean', 'stage'], stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, universal_newlines=True)
    for line in proc.stdout:
        log(f" | {line}")
    proc.wait()


def start_first_node():
    log("-- Starting first fruitarian node 0\n")
    proc = subprocess.Popen(['./target/universal/stage/bin/fruitarian'], stdout=subprocess.PIPE,
                            stderr=subprocess.STDOUT, universal_newlines=True)
    return proc


def add_node(nr, server_port, known_port):
    log(f"-- Starting fruitarian node {nr}\n")
    proc = subprocess.Popen(['./target/universal/stage/bin/fruitarian', str(server_port), 'localhost', str(known_port)],
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
    return proc


def parse_args():
    parser = argparse.ArgumentParser(
        description="Run the Fruitarian project")

    parser.add_argument('-n', '--nodes', type=int, required=True,
                        help="number of nodes to start")
    parser.add_argument('-s', '--skip', action='store_true',
                        help="skip packaging of the Scala project")
    return parser.parse_args()


def stop(sig, frame):
    print("-- Stopping...")
    for proc in procs:
        proc.terminate()
    sys.exit(0)


def main():
    args = parse_args()
    signal.signal(signal.SIGINT, stop)

    if not args.skip:
        package()

    if args.nodes < 1:
        sys.exit("At least one node should be started")

    first_node = start_first_node()
    procs.append(first_node)
    time.sleep(1)

    for i in range(1, args.nodes):
        proc = add_node(i, FIRST_PORT + i, FIRST_PORT + i - 1)
        procs.append(proc)
        time.sleep(1)

    while True:
        for i, proc in enumerate(procs):
            line = proc.stdout.readline()
            if proc.poll() is not None:
                break
            if line:
                log(f"[{i}] | {line}")


if __name__ == '__main__':
    main()
