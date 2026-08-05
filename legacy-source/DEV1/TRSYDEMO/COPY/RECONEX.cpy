000100 01  RECON-EXCEPTION-RECORD.
000200     05  RX-PAYMENT-ID              PIC X(12).
000300     05  FILLER                     PIC X VALUE '|'.
000400     05  RX-BANK-TRACE              PIC X(15).
000500     05  FILLER                     PIC X VALUE '|'.
000600     05  RX-CODE                    PIC X(2).
000700     05  FILLER                     PIC X VALUE '|'.
000800     05  RX-EXPECTED-AMOUNT         PIC 9(13)V99.
000900     05  FILLER                     PIC X VALUE '|'.
001000     05  RX-ACTUAL-AMOUNT           PIC 9(13)V99.
001100     05  FILLER                     PIC X VALUE '|'.
001200     05  RX-REASON                  PIC X(40).
001300     05  RX-SOURCE                  PIC X(16).
