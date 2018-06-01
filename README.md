# FEUP-SDIS
This repository hosts our projects for the Distributed Systems (SDIS) course unit at FEUP.

## First Project
A distributed backup system, featuring dynamic data duplication, and other reliability protocols. More detailed information [here](Proj1/README.md).

## Second Project
A peer-to-peer network for distributing adversarial search computations. This type of tasks is a great candidate for distributing over a network, as it is highly parallelizable, and all tasks obey a short common interface (providing the _successors_, _utility_, and _is_final_ functions).

Peers communicate through our message protocol, and expose an RMI interface for authenticated clients to submit tasks for computation. We also made a small example for the TicTacToe game.

A more detailed overview of the project can be seen in the [report](Proj2/docs/report.pdf).

## Contributors
* [AndreFCruz](https://github.com/AndreFCruz)
* [antonioalmeida](https://github.com/antonioalmeida)
* [diogotorres97](https://github.com/diogotorres97)
* [cyrilico](https://github.com/cyrilico)