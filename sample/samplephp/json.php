<?php

session_start();

if( isset($_SESSION['user']) && !empty($_SESSION['user'])){
	//already logged in..
	$user = $_SESSION['user'];
}else{
	header('location: index.php?loginfailed');
	exit(-1);
}

$htoken = $_SESSION['htoken'];

$params = json_decode(file_get_contents('php://input'), true); 

$prevtoken = $params['X-SPECIAL'];
if($prevtoken !== $htoken ){
    header('location: index.php?htokenerr');
    exit(-1);
}

header("Content-Type: application/json");

$params['RESULT'] = "OK";

print json_encode($params);

?>









