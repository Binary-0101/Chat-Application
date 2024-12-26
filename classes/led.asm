ARM Assembly Language Program To Add Some Data and Store the SUM in R3. 
 
AREA ADD, CODE, READONLYENTRY 
MOV 
MOV 
ADD 
R1, #0x25 
R2, #0x34 
R3, R2, R1 
; R1 = 0x25 
; R2 = 0x34 
; R3 = R2 + R1 
HERE B HERE ; stay here forever 
END   
 
 
 
 
 
 
 
Program 1(B). SUB 
 
AREA SUB, CODE, READONLYENTRY 
MOV R2, #4 ; R2 = 4 
MOV R3, #2 ; R3 = 2 
MOV R4, #4 ; R4 = 4 
SUBS R5, R2, R3 ; R5 = R2 - R3 (R5 = 4 - 2 = 2) 
SUBS R5, R2, R4 ; R5 = R2 - R4 (R5 = 4 - 4 = 0) 
END  