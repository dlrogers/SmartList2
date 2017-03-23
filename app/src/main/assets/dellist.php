<?php
// @ Copyright 2017 Dennis Rogers
//
//	auth.php: Code to create user authentication info.
//
//		inputs(POST):	command, email, password, listname (Strings)
//
//		command			Function
//
//		"new":	Set's up new user by adding email, password, and listname
//				to users table in the database admin and creating a new
//				database named with the email.
//		"add":	Adds a new list if it doesn't already exist by adding
//				email,password,and listname to users
//				and adding new table, named listname, to the database
//
// Set up php
ini_set("log_errors",1) ;
ini_set("error_log","/tmp/php_error.log");
ini_set("max_execution_time",5);
//ini_set("ignore_user_abort",1);
error_reporting(E_ALL);

logError("starting dellist");

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
logError("SELECT * from users where email='".$email."' AND list='".$list."'");
$rslt=$db->query("SELECT * from users where email='".$email."' AND list='".$list."'");
if($rslt==false){
	logError("select error: ".$db->error);
	exit();
}
$row=$rslt->fetch_assoc();
if($row!=null) {
	db_query("DELETE FROM users WHERE list='".$list."'");
	db_query("use `".$email."`");
	db_query("DROP TABLE IF EXISTS `".$list."`");
} else
	print("nok");

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