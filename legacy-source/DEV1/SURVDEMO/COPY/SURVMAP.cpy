000100 01  SURVSCRI.
000200     05  FILLER                     PIC X(12).
000300     05  CLAIMIDL                   PIC S9(4) COMP.
000400     05  CLAIMIDF                   PIC X.
000500     05  FILLER REDEFINES CLAIMIDF.
000600         10  CLAIMIDA               PIC X.
000700     05  CLAIMIDI                   PIC X(12).
000800     05  BENEIDL                    PIC S9(4) COMP.
000900     05  BENEIDF                    PIC X.
001000     05  FILLER REDEFINES BENEIDF.
001100         10  BENEIDA                 PIC X.
001200     05  BENEIDI                     PIC X(10).
001300 01  SURVSCRO.
001400     05  FILLER                     PIC X(12).
001500     05  CLAIMIDO                   PIC X(12).
001600     05  BENEIDO                    PIC X(10).
001700     05  BENENMO                    PIC X(30).
001800     05  RELATIONO                  PIC X(10).
001900     05  MONTHAMTO                  PIC X(15).
002000     05  STARTDTO                   PIC X(10).
002100     05  ENDDTO                     PIC X(10).
002200     05  STATUSO                    PIC X(12).
002300     05  MESSAGEO                   PIC X(60).
