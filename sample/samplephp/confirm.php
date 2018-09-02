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
$filetype = $_FILES['imgfile']['type'];

$savepath = session_save_path();

$mailfile = $savepath . "/mail.txt";
$oldmail = file_get_contents($mailfile);

if ( empty($subject) ||
     empty($contents) ||
     empty($mailaddr) || $mailaddr === $oldmail){
     $_SESSION['token2'] = $randomval;
?>

<html>
<head>
<tltle>
inquiry input:お問い合わせ入力
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>

<form action="confirm.php" method="POST">
<input type="hidden" name="token2" value="<?php echo $randomval; ?>">
件名<input type="text" name="subject" value="<?php echo $subject; ?>"><BR>
お問い合わせ内容your inquiry<BR>
<TEXTAREA name="contents" rows="4" cols="40"><?php echo $contents; ?></textarea><BR>
mail<BR>
<?php
if ($mailaddr === $oldmail){
    print "<P> 登録済みメールアドレスです。 mailaddr has already registered.\n"; 
}
?>
<input type="text" name="mailaddr" value="<?php echo $mailaddr; ?>" size="10" ><BR>
<input type="submit"  value="confirm:確認">
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

$uploadTo = "/tmp/$imgfile";
if(move_uploaded_file($tmp_path, $uploadTo)==TRUE){
   $_SESSION['showfile'] = 'showfile.php?path=' . $uploadTo;
   $_SESSION['filetype'] = $filetype;
}


?>
<html>
<head>
<tltle>
inquiry confirm:お問い合わせ確認
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>

<form action="complete.php" method="POST">
<input type="hidden" name="token3" value="<?php echo $randomval; ?>">
下記の内容をご確認の上、送信ボタンを押してください。confirm your input below.<BR>
<table border="1">
<tr>
<th>件名subject</th><td><?php echo $subject; ?></td>
</tr>
<tr>
<th>お問い合わせ内容message</th><td><?php echo $contents; ?></td>
</tr>
<tr>
<th>宛先mailaddr</th><td><?php echo $mailaddr; ?></td>
</tr>
<tr>
<th>ファイルfile</th><td><?php echo $imgfile; ?></td>
</tr>
<tr>
<th>ファイル格納先file stored path</th><td><?php echo $tmp_path; ?></td>
</tr>
</table>

<input type="submit"  value="complete">
</form>

<P>
<A HREF="logout.php">logout</A>
</body>
</html>



