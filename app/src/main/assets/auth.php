<?php
// @ Copyright 2017 Dennis Rogers

// Set up php
ini_set("log_errors",1) ;
ini_set("error_log","/tmp/php_error.log");
ini_set("max_execution_time",5);
//ini_set("ignore_user_abort",1);
error_reporting(E_ALL);

logError("starting auth");

// Get username and password for mysql and log in
$config = parse_ini_file("/home/dennis/Mydocs/config.ini");
$db = new mysqli('localhost',$config['username'],$config['password']);
$db->set_charset("UTF-8");

// Open data stream and read in command, uid, pw, and list
$std = fopen("php://input","r");
$email=sscanf(fgets($std),"%s")[0];
$passwd=sscanf(fgets($std),"%s")[0];
$list=sscanf(fgets($std),"%s")[0];
logError($email.",".$passwd.",".$list);
// logError("select * from users where email='".$email."'");
$db->query("use admin");
$rslt=$db->query("select * from users where email='".$email."'");
if($rslt==false) logError("rslt is false");
$row=$rslt->fetch_assoc();
if($row!=null) {
	print("exists");
} else {
	db_query("INSERT INTO users VALUES('".$email."','".$list."','".$passwd."')");
	db_query("CREATE DATABASE `".$email."`");
	db_query("use `".$email."`");
	db_query("CREATE TABLE `".$list."`(name varchar(40),flags int,last_time int,last_avg int,ratio real)");
	print("ok");
}
register_shutdown_function('shutdown');
function shutdown(){
	exit();
}
function db_query($cmd) {
	global $db;
	logError($cmd);
	return $db->query($cmd);
}
function logError($str){
	error_log($str,0);
}

?>
