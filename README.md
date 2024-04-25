# Home Assignment - Log Reading System
A home assignment where I was required to develop a log reading system where patterns can be searched in long log files.

The system includes:
     
DB that will hold the searched logs with the following parameters:

Log file name.
Patterns that were found for this log file (if were found).
Size of log file in MB.

REST server with the following operations:

Check for patterns in log file and add to DB after was searched.
Input: log file
Get names of logs files that were searched.
Delete log from DB. Input: log file name or id.

Note: Server must work asynchronously.

Queue for DB requests – only two requests can be addressed simultaneously. All other requests should wait in queue and be pulled by FIFO principle. 



# I created this project using some of the knowledge I gained in:
- Java & SpringBoot
- Docker
- Data Structures (B-Tree)
- Multithreading
- JUnit

Thank you for your time 😁
