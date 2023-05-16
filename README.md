# Nand2Tetris: Jack Compiler
This repository holds the complete implementation of the Jack Compiler, part of the Nand2Tetris course. The compiler is capable of translating high-level Jack code into VM code.

## About Nand2Tetris
Nand2Tetris is an incredible self-study course that guides you through the construction of a modern computer system, starting from first principles. One of the main components of this journey is building a compiler for the Jack language, a simple, Java-like object-oriented programming language.

## About the Jack Compiler
The Jack Compiler is built in two stages:

Syntax Analyzer (Tokenizer and Parser)
Code Generation
The Syntax Analyzer breaks down Jack source code into its constituent grammatical elements, generating an abstract syntax tree. The Code Generation stage then traverses this syntax tree, outputting corresponding VM code.

## Usage
1. Clone this repository: `https://github.com/jcob-sikorski/JackCompilator.git`
2. Navigate to the directory containing the source Jack files: `cd /path-to-your-jack-files`
3. Run the Jack Compiler.
4. This will generate .vm files for each .jack file in the directory.

## Acknowledgements
This project was part of the Nand2Tetris course - an excellent resource for learning about computer systems from the ground up.
