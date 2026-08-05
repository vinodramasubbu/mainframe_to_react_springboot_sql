000100 01  ACCTSCRI.
000200     05  FILLER                     PIC X(12).
000300     05  ACCTIDL                    PIC S9(4) COMP.
000400     05  ACCTIDF                    PIC X.
000500     05  FILLER REDEFINES ACCTIDF.
000600         10  ACCTIDA                PIC X.
000700     05  ACCTIDI                    PIC X(10).
000800 01  ACCTSCRO.
000900     05  FILLER                     PIC X(12).
001000     05  ACCTIDO                    PIC X(10).
001100     05  CUSTNMO                    PIC X(30).
001200     05  TYPEO                      PIC X(3).
001300     05  BALANCEO                   PIC X(15).
001400     05  STATUSO                    PIC X(10).
001500     05  MESSAGEO                   PIC X(60).
