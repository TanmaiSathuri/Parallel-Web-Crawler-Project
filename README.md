Parallel Web Crawler

📌 Project Overview:

This project implements a **Parallel Web Crawler** in Java that efficiently crawls multiple web pages concurrently using **multithreading**, while respecting depth limits, timeouts, and parallelism constraints.

🛠️ Technologies Used:

-  Java
 
-  Maven
  
-  Multithreading
   
-  Parallel Streams  

✨ Features:

-  Crawls multiple web pages concurrently
  
-  Respects crawl depth and timeout limits
    
-  Efficient thread management
  
-  Tested using Maven  

 ▶️ How to Run:

1. Clone the repository:
   git clone https://github.com/TanmaiSathuri/Parallel-Web-Crawler-Project.git

2. Navigate to the project directory:
   cd webcrawler

3. Build the project:
   mvn clean compile

4. Run the application:
   mvn exec:java -Dexec.mainClass=com.udacity.webcrawler.WebCrawlerMain
