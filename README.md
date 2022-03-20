<h1>My HTTP server</h1>
<p>To launch the server you need to download the project, build Dockerfile, and 
then run this docker file with flags (-p 8000:8000). After these 
steps, server is set up on your machine. You can connect to it via netcat 
(nc 0.0.0.0 8000). And at this point you can pass HTTP requests. 
For example: </p>
<p>GET /n1 HTTP/1.1</p>
<p>Connection: keep-alive</p>
<p>As you can notice there are two text files containing numbers in the project:
n1 and n2. You can choose one of them or add your own text file to the 
project (in the root directory) before building the image. To implement pipeline 
I decided to fetch computed HTTP responses after each entered space.
So each time you insert a blank line, the program checks if it has 
any new computed HTTP responses in its queue. Each request must be 
separated by at least one empty line. So you pass HTTP requests, and then 
press enter until response is received.</p>