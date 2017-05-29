<?php

session_start();

if( isset($_SESSION['user'])){
	//already logged in..
	$user = $_SESSION['user'];
}else{
	header('location: index.php');
	exit(-1);
}

$prevtoken = $_POST['token2'];
$chktoken = $_SESSION['token2'];

if($prevtoken !== $chktoken ){
    header('location: index.php');
    exit(-1);
}

$randomval = sha1(uniqid(rand(), true));




$subject = $_POST['subject'];
$contents = $_POST['contents'];
$mailaddr = $_POST['mailaddr'];
$imgfile = $_FILES['imgfile']['name'];
$tmp_path = $_FILES['imgfile']['tmp_name'];

if ( empty($subject) ||
     empty($contents) ||
     empty($mailaddr) ){
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

<form action="confirm.php" method="POST">
<input type="hidden" name="token2" value="<?php echo $randomval; ?>">
件名<input type="text" name="subject" value="<?php echo $subject; ?>"><BR>
お問い合わせ内容<BR>
<TEXTAREA name="contents" rows="4" cols="40"><?php echo $contents; ?></textarea><BR>
mail<BR>
<input type="text" name="mailaddr" value="<?php echo $mailaddr; ?>" size="10" ><BR>
<input type="submit"  value="確認">
</form>

<P>
<A HREF="logout.php">logout</A>
</body>
</html>
<?php
    exit(0);
}

$_SESSION['token3'] = $randomval;
$_SESSION['subject'] =  $subject;
$_SESSION['contents'] =  $contents;
$_SESSION['mailaddr'] =  $mailaddr;
$_SESSION['imgfile'] =  $imgfile;
$_SESSION['tmp_path'] =  $tmp_path;

?>
<html>
<head>
<tltle>
お問い合わせ確認
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>

<form action="complete.php" method="POST">
<input type="hidden" name="token3" value="<?php echo $randomval; ?>">
下記の内容をご確認の上、送信ボタンを押してください。<BR>
<table border="1">
<tr>
<th>件名</th><td><?php echo $subject; ?></td>
</tr>
<tr>
<th>お問い合わせ内容</th><td><?php echo $contents; ?></td>
</tr>
<tr>
<th>宛先</th><td><?php echo $mailaddr; ?></td>
</tr>
<tr>
<th>ファイル</th><td><?php echo $imgfile; ?></td>
</tr>
<tr>
<th>ファイル格納先</th><td><?php echo $tmp_path; ?></td>
</tr>
</table>

<input type="submit"  value="送信">
</form>

<P>
<A HREF="logout.php">logout</A>
</body>
</html>



