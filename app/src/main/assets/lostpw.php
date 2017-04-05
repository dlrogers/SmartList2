<?php
/* Routine to send lost password email

    Input: Cust Email address

*/
ini_set("log_errors",1) ;
ini_set("error_log","/tmp/php_error.log");
ini_set("max_execution_time",5);

// Open data stream and read in customer's email address
$std = fopen("php://input","r");
$email=sscanf(fgets($std),"%s")[0];

$message = '
<html>
<head>
In order to reset your password click on the following link.
<a href="http://192.168.1.209"> Reset Link. </a>


';
// To send HTML mail, the Content-type header must be set
$h = 'MIME-Version: 1.0\r\n';
$h = $h.'Content-type: text/html; charset=iso-8859-1\r\n';

// Additional headers
$h = $h.'To: SmartList User\r\n';
$h = $h.'From: Support Team';
logError($message);
logError($h);
$email="dlrog@yahoo.com";
mail($email,"Lost Password Reset",$message,$h);

function logError($str){
	error_log($str,0);
}
?>