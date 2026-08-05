000100 01  TXN-VALIDATION-AREA.
000200     05  TV-TRANSACTION.
000300         10  TV-TXN-ID               PIC X(10).
000400         10  TV-ACCOUNT-ID           PIC X(10).
000500         10  TV-TXN-DATE             PIC X(8).
000600         10  TV-TXN-TYPE             PIC X(1).
000700         10  TV-TXN-AMOUNT           PIC X(11).
000800         10  TV-REFERENCE            PIC X(13).
000900     05  TV-RESULT                   PIC X(1).
001000         88  TV-RESULT-VALID         VALUE '0'.
001100         88  TV-RESULT-INVALID       VALUE '1'.
001200     05  TV-REASON-CODE              PIC X(2).
