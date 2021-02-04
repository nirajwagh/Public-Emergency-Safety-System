var express= require("express");
var app=express();

app.set('view engine', 'ejs');

app.use(express.static('public'));

app.get("/", function(req, res){
    res.render("login");
}).listen(3000);

app.get("/map", function(req, res){
    res.render("map");
});

app.get("/complaints", function(req, res){
    res.render("complaints");
});




console.log("Server Started");
