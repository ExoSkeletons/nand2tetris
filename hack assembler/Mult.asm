// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
// The algorithm is based on repetitive addition.


// res = 0
@res
M = 0

// zero checks (R0/R1 = 0 -> res = 0, skip calc)
@R0
D = M
@loopExit
D ; JEQ
@R1
D = M
@loopExit
D ; JEQ


// to help with loop count effiencey, we'll set the added value to be the higher of the two,
// and the loop counter to be the lower one.
@R0
D = M
@R1
D = D - M // D = R0 - R1
@R1GTR0
D ; JLT // R0 - R1 < 0 -> R0 < R1
// R0 >= R1: count = R1, adder = R0
(R0GER1)
@R0
D = M
@count
M = D
@R1
D = M
@adder
M = D
@loopInit
0 ; JMP
// R0 < R1: count = R0, adder = R1
(R1GTR0)
@R0
D = M
@count
M = D
@R1
D = M
@adder
M = D
@loopInit
0 ; JMP


// addition loop
(loopInit)
// i = 0
@i
M = 0
// loop
(addLoop)

    // exit condition (i >= count, 0 >= count - i)
    @i
    D = M
    @count
    D = M - D
    @loopExit
    D; JLE

    // add
    // res += adder
    @adder
    D = M
    @res
    M = M + D

    // i++
    @i
    M = M + 1
    // loop
    @addLoop
    0 ; JMP

(loopExit)

// R2 = res
@res
D = M
@R2
M = D


(END)
@END
0 ; JMP