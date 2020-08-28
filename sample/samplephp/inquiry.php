<?php


session_start();

include 'clearSessionTokens.php';

if (isset($_POST['cancel'])) {
    clearSessionTokens();
    unset($_SESSION['subject']);
    unset($_SESSION['contents']);
    unset($_SESSION['mailaddr']);
    unset($_SESSION['imgfile']);
    unset($_SESSION['tmp_path']);
    unset($_SESSION['showfile']);
    unset($_SESSION['filetype']);
    header('location: mypage.php');
    exit(-1);
}

if( isset($_SESSION['user'])){
	//already logged in..
	$user = $_SESSION['user'];
}else{
	header('location: index.php');
	exit();
}

$prevtoken = $_GET['token1'];
$chktoken = $_SESSION['token1'];

if(empty($chktoken) || $prevtoken !== $chktoken ){
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
inquiry input
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>

<form action="confirm.php" method="POST" enctype="multipart/form-data">
<input type="hidden" name="token2" value="<?php echo $randomval; ?>">
Subject<input type="text" name="subject" value="<?php echo $subject;?>"><BR>
Contents<BR>
<TEXTAREA name="contents" rows="4" cols="40"><?php echo $contents; ?></textarea><BR>
mail<BR>
<input type="text" name="mailaddr" value="<?php echo $mailaddr; ?>" size="10" ><BR>
<input type="file" name="imgfile" accept="image/*" ><P>
<input type="submit"  value="Confirm">
</form>
<P>
<form action="inquiry.php" method = "POST">
<INPUT type="HIDDEN" name="cancel" value="1">
<input type="submit"  value="Cancel">
</form>

<P>
<A HREF="logout.php">logout</A>
</body>
</html>



