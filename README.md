# Concurrent Boolean Circuit Evaluator
A project completed for Concurrent Programming course during 3rd semester of Bachelor's Degree in Computer Science.

This project implements concurrent evaluation of Boolean expressions represented as tree-structured circuits. It supports parallel computation of subexpressions, lazy evaluation (short-circuiting), and safe interruption.

## Features

- Supports expressions: `true`, `false`, `NOT`, `AND`, `OR`, `IF`, `GTx`, `LTx`
- Concurrent evaluation of single and multiple circuits
- Short-circuit logic (e.g., `OR(true, x)` skips `x`)
- Safe interruption via `stop()`
