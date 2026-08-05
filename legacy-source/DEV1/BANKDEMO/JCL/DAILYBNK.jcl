//DAILYBNK JOB (BANK),'BANK DAILY POST',CLASS=A,MSGCLASS=X,
//             NOTIFY=&SYSUID,REGION=0M
//* -----------------------------------------------------------------
//* SORT SOURCE TRANSACTIONS INTO ACCOUNT/DATE/ID ORDER.
//* EBCDIC COLLATION IS PART OF THE OBSERVED OUTPUT ORDER.
//* -----------------------------------------------------------------
//SORTTXN  EXEC PGM=SORT
//SYSOUT   DD SYSOUT=*
//SORTIN   DD DSN=BANKDEMO.DAILY.TXNIN,DISP=SHR
//SORTOUT  DD DSN=&&SORTED,DISP=(NEW,PASS,DELETE),
//            SPACE=(CYL,(5,2),RLSE),
//            DCB=(RECFM=FB,LRECL=53,BLKSIZE=0)
//SYSIN    DD DSN=DEV1.BANKDEMO.CNTL(TRNSORT),DISP=SHR
//* -----------------------------------------------------------------
//* POST TO DB2. RETURN CODE 4 MEANS BUSINESS REJECTS WERE PRODUCED.
//* RETURN CODE 12 MEANS TECHNICAL FAILURE AND REQUIRES OPERATOR REVIEW.
//* -----------------------------------------------------------------
//BATCH    EXEC BANKBAT,
//         INFILE='&&SORTED',
//         AUDIT='BANKDEMO.DAILY.AUDIT(+1)',
//         REJECT='BANKDEMO.DAILY.REJECT(+1)'
//* -----------------------------------------------------------------
//* PUBLISH COMPLETION FLAG ONLY FOR RC 0 OR 4.
//* -----------------------------------------------------------------
// IF (BATCH.POST.RC LE 4) THEN
//PUBLISH  EXEC PGM=IEFBR14
//DONE     DD DSN=BANKDEMO.DAILY.DONE(+1),
//            DISP=(NEW,CATLG,DELETE),
//            SPACE=(TRK,(1,1),RLSE),
//            DCB=(RECFM=FB,LRECL=80,BLKSIZE=0)
// ENDIF
