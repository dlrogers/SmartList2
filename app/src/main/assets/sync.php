<?php
ini_set("log_errors",1) ;
ini_set("error_log","/tmp/php_error.log");
error_reporting(E_ALL);
logError("starting sync");
$config = parse_ini_file("/home/dennis/Mydocs/config.ini");
//logError("username=".$config['username'].", passwd =".$config['password']);
$db = new mysqli('localhost',$config['username'],$config['password']);
$db->set_charset("UTF-8");
$std = fopen("php://input","r");
// $cmd=sscanf(fgets($std),"%s")[0];
$email=sscanf(fgets($std),"%s")[0];
$passwd=sscanf(fgets($std),"%s")[0];
$list=sscanf(fgets($std),"%s")[0];
logError($email.",".$passwd.",".$list);;
//logError("select * from users where email='".$email."'");
$db->query("use admin");
$rslt=$db->query("select * from users where email='".$email."'");
$row=$rslt->fetch_assoc();
if($db->select_db($email)){
	$db->query("use `".$email."`");
	logError($email." exists");
	print("exists");
	//			sync($email,$list,$passwd);
	$db->query("use admin");
	$sql="select * from users where email=`".$email."` AND list='".$list."'";
	logError($sql);
	$db->query($sql);
	$sql="use `".$email."`";
	logError($sql);
	$db->query($sql);
	//			logError(sprintf("email = %s, passwd = %s, list = %s",$email,$passwd,$list));
	$names=array();
	$n=0;
	while($txt=fgets($std)) {				//Get an item from phone
		logError($txt);
		$txt=trim($txt,"\n");				//remove returns
		$p_cols=explode(',',$txt);			//separate into columns (name,inList,last_time,last_avg,ration)
		$p_cols[0]=$p_cols[0];
		$rslt=$db->query("SELECT * from ".$list." where name=" . //Search server for item
				sprintf("'%s'",$p_cols[0]));
		logError($db->error);
		$names[$n]=$p_cols[0];					//Save names in $names[]
		logError(sprintf("Read %s",$p_cols[0])) ;
		if($rslt!=false){
			//		printf("name = %s, count = %d\n",$p_cols[0],count($rslt));
			logError("result is true");
			$row=$rslt->fetch_assoc();
			$plt=sscanf($p_cols[2],"%s")[0];
			if($row==null) {		//if(does not exist on server add it)
				logError("row is null");
				$db->query(sprintf("INSERT INTO itemDb VALUES ('%s',%s,%s,%s,%s)",
						$p_cols[0],$p_cols[1],$p_cols[2],$p_cols[3],$p_cols[4]));
			} else {					//else check if changed
				if(!$pChg) {	// if phone has not been changed
					logError("pChg=0");
					printf("%s,%s,%s,%s,%s,%s\n",
							"u",$row['name'],$row['inList'],$row['last_time'],$row['last_avg'],$row['ratio']);
				}
				else {		// If phone copy changed: update server db
					logError("pChg=1");
					$sql=sprintf(
							"UPDATE itemDb SET last_time=%s,last_avg=%s,ratio=%s,inList=%s WHERE name='%s'",
							$plt,$p_cols[3],$p_cols[4],$p_cols[1],$p_cols[0]);
					logError($sql);
					$rslt=$db->query($sql);
					if(!$rslt) logError("dB update failed") ;
				}
			}
			$n+=1;
		}
	}
}
else {
	logError($email." does not exist");
	if(!$db->query("INSERT INTO users VALUES('".$email."','".$list."','".$passwd."')"))
		logError("insert error");
		$rc=$db->query("CREATE DATABASE `".$email."`");
		$rc=$db->query("USE `".$email."`");
		$sql="CREATE TABLE `".$list."`(name TEXT, flags INT, last_time INT, last_avg INT, ratio REAL)";
		$rc=$db->query($sql);
		print("created");
}
logError("done");

function logError($str){
	error_log($str,0);
}
?>