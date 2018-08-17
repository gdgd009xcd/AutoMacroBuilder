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
$headers = apache_request_headers();
$prevtoken = "";
foreach ($headers as $header => $value) {
    error_log("[" . $header . "->" . $value . "]");
    if($header === "X-SPECIAL"){
        $prevtoken = $value;
        break;
    }
}

if($prevtoken !== $htoken ){
    header('location: index.php?htokenerr');
    exit(-1);
}



?>
<html>
<head>
<tltle>
ポップアップ
</tltle>
</head>
<body>
<P>
your X-SPECIAL header is valid:[<?php echo $htoken; ?>]<BR>
<P>
<A HREF="mypage.php">マイページに戻る</A><BR>
<P>
<A HREF="logout.php">logout</A>
</body>
</html>



