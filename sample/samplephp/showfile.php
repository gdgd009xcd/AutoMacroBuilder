<?php

session_start();

if( isset($_SESSION['user'])){
	//already logged in..
	$user = $_SESSION['user'];
}else{
	header('location: index.php');
	exit(-1);
}

$showfile = $_SESSION['showfile'];

$path = $_GET['path'];
$filetype = $_SESSION['filetype'];

header("Content-Type: " . $filetype);

readfile($path);

exit();
?>