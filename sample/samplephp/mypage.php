<?php 

if(isset($_POST['user'])){
    $user = $_POST['user'];
}else{
    $user ="";
}

if(isset($_POST['pass'])){
    $pass = $_POST['pass'];
}else{
    $pass = "";
}

session_start();

if ( $user === "daike" && $pass === "test1234"){
	$_SESSION['user'] = $user;
}elseif( isset($_SESSION['user'])){
	//already logged in..
	$user = $_SESSION['user'];
}else{
	header('location: index.php');
	exit();
}

$randomval = sha1(uniqid(rand(), true));

$_SESSION['token1'] = $randomval;

?>
<html>
<head>
<tltle>
MYPAGE
</tltle>
</head>
<body>

<P> welcome user:<?php echo $user; ?>

<form action="inquiry.php" method="GET">
<input type="hidden" name="token1" value="<?php echo $randomval; ?>">
<input type="submit"  value="お問い合わせ">
</form>

<P>
<A HREF="logout.php">logout</A>
</body>
</html>



