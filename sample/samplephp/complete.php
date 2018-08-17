<?php

session_start();

if( isset($_SESSION['user'])){
	//already logged in..
	$user = $_SESSION['user'];
}else{
	header('location: index.php');
	exit(-1);
}

$prevtoken = $_POST['token3'];
$chktoken = $_SESSION['token3'];

if($prevtoken !== $chktoken ){
    header('location: index.php');
    exit(-1);
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
if(isset($_SESSION['imgfile'])){
    $imgfile = $_SESSION['imgfile'];
}else{
    $imgfile = "";
}

if(isset($_SESSION['showfile'])){
    $showfile = $_SESSION['showfile'];
}else{
    $showfile = "";
}

if ( empty($subject) ||
     empty($contents) ||
     empty($imgfile) ||
     empty($mailaddr) ){
     $_SESSION['token1'] = $randomval;
     header('Location: inquiry.php?token1=' . $randomval);
    exit(0);
}

$savepath = session_save_path();
$mailfile = $savepath . "/mail.txt";
$oldmail = file_put_contents($mailfile, $mailaddr);



?>
<html>
<head>
<tltle>
inquiry completed:お問い合わせ　完了
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>
<?php
echo "<P>fileput result:" . $oldmail . "<P><BR>";
?>
お問い合わせを下記の内容で受け賜りました。We accepted your inquiry with the contents below. <BR>
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
<th>ファイルfile</th><td><A HREF="<?php echo $showfile; ?>" ><?php echo $imgfile; ?></A></td>
</tr>
</table><BR>

<A HREF="mypage.php">マイページに戻るgo back to mypage</A><BR>
<P>
<A HREF="logout.php">logout</A>
</body>
</html>



