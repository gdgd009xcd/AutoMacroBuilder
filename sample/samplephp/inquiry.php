<?php


session_start();

if( isset($_SESSION['user'])){
	//already logged in..
	$user = $_SESSION['user'];
}else{
	header('location: index.php');
	exit();
}

$prevtoken = $_GET['token1'];
$chktoken = $_SESSION['token1'];

if($prevtoken !== $chktoken ){
    header('location: index.php');
    exit();
}

$randomval = sha1(uniqid(rand(), true));

if(isset($_SESSION['subject'])){
    $subject = $_SESSION['subject'];
}else{
    $subject = "";
}

if(isset($_SESSION['contents'])){
    $contents = $_SESSION['contents'];
}else{
    $contents = "";
}

if(isset($_SESSION['mailaddr'])){
    $mailaddr = $_SESSION['mailaddr'];
}else{
    $mailaddr = "";
}

$_SESSION['token2'] = $randomval;

?>
<html>
<head>
<tltle>
お問い合わせ入力
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>

<form action="confirm.php" method="POST" enctype="multipart/form-data">
<input type="hidden" name="token2" value="<?php echo $randomval; ?>">
件名<input type="text" name="subject" value="<?php echo $subject;?>"><BR>
お問い合わせ内容<BR>
<TEXTAREA name="contents" rows="4" cols="40"><?php echo $contents; ?></textarea><BR>
mail<BR>
<input type="text" name="mailaddr" value="<?php echo $mailaddr; ?>" size="10" ><BR>
<input type="file" name="imgfile" accept="image/*" ><P>
<input type="submit"  value="確認">
</form>

<P>
<A HREF="logout.php">logout</A>
</body>
</html>



