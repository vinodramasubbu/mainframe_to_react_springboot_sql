000100 01  TXN-INPUT-RECORD.
000200     05  TXN-ID                     PIC X(10).
000300     05  TXN-ACCOUNT-ID             PIC X(10).
000400     05  TXN-DATE                   PIC 9(8).
000500     05  TXN-TYPE                   PIC X(1).
000600         88  TXN-CREDIT             VALUE 'C'.
000700         88  TXN-DEBIT              VALUE 'D'.
000800     05  TXN-AMOUNT-DISPLAY         PIC 9(9)V99.
000900     05  TXN-REFERENCE              PIC X(13).
