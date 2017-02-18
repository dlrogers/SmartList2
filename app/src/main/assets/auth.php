<?php
// @ Copyright 2017 Dennis Rogers

ini_set("log_errors",1) ;
ini_set("error_log","/tmp/php_erroradmin.log");
ini_set("max_execution_time",5);
//ini_set("ignore_user_abort",1);
error_reporting(E_ALL);
logError("starting auth");
$config = parse_ini_file("/home/dennis/Mydocs/config.ini");
//logError("username=".$config['username'].", passwd =".$config['password']);
$db = new mysqli('localhost',$config['username'],$config['password']);
$db->set_charset("UTF-8");
$std = fopen("php://input","r");
// $cmd=sscanf(fgets($std),"%s")[0];
$cmd=sscanf(fgets($std),"%s")[0];
$email=sscanf(fgets($std),"%s")[0];
$passwd=sscanf(fgets($std),"%s")[0];
$list=sscanf(fgets($std),"%s")[0];
logError($email.",".$passwd.",".$list);
// logError("select * from users where email='".$email."'");
$db->query("use admin");
$rslt=$db->query("select * from users where email='".$email."'");
if($rslt==false) logError("rslt is false");
$row=$rslt->fetch_assoc();
if(row!=null) {
	print("exists");
} else {
	$db->query("INSERT INTO users VALUES('".$email."','".$list."','".$passwd."')");
	$db->query("CREATE DATABASE '".$email."'");
	print("ok");
}
register_shutdown_function('shutdown');
function shutdown(){
	logError("shutdown");
	exit();
}
function logError($str){
	error_log($str,0);
}

?>
