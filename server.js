// Node.js frontend server
// Run: node server.js
// Then open: http://localhost:3000
const http=require("http"),fs=require("fs"),path=require("path");
const port=3000;
const types={".html":"text/html",".css":"text/css",".js":"text/javascript"};
http.createServer((req,res)=>{
  let p=req.url.split("?")[0];
  if(p==="/")p="/index.html";
  const file=path.join(__dirname,p);
  fs.readFile(file,(err,data)=>{
    if(err){res.writeHead(404,{"Content-Type":"text/plain"});res.end("File not found");return;}
    res.writeHead(200,{"Content-Type":types[path.extname(file)]||"application/octet-stream"});
    res.end(data);
  });
}).listen(port,()=>console.log(`HallBook frontend running at http://localhost:${port}`));
