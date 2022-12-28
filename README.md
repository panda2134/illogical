# Illogical (WIP)

Easy-to-use first order logic tools.

## Features

- [x] Formula transformations
	- [x] Express in terms of AND, OR, NOT
	- [x] De Morgan's laws
	- [x] Quantifier negation
	- [x] Conjunctive normal form (CNF)
	- [x] Prenex normal form (PNF)
	- [x] Substitute variables, etc.
- [x] Skolemization
	- [x] Substitute Skolem's function
- [x] Unification
	- [x] Most general unifier (MGU)
- [ ] Semantic tableaux method (MTS)
- [ ] Resolution method
- [x] CLI client
- [ ] Web client

## How to run

First, install scala & sbt. You can use the command `nix develop` if using Nix.

After that, execute `sbt run` in the terminal.

## How to test

```sh
sbt ~test
```

## Grammar

```
Qu      → ∀ | ∃
Op      → ∧ | ∨ | …
Id      → [a-z]+[0-9]*[']*
Con     → @Id
Var     → Id
Func    → Id
Pred    → Id

Args    → Term | Term, Args → List(Term)
Term    → Con | Var | Func(Args)
Atom    → Pred(Args)
Form    → Atom | ¬Form | Form Op Form | Qu Var Form
Literal → Atom | ¬Atom
Clause  → Literal ∨ Literal
```
