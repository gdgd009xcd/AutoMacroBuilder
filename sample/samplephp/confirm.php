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
	exit(-1);
}

$prevtoken = $_POST['token2'];
$chktoken = $_SESSION['token2'];

if(empty($chktoken) || $prevtoken !== $chktoken ){
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
inquiry Input
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>

<form action="confirm.php" method="POST">
<input type="hidden" name="token2" value="<?php echo $randomval; ?>">
Subject<input type="text" name="subject" value="<?php echo $subject; ?>"><BR>
your inquiry contents<BR>
<TEXTAREA name="contents" rows="4" cols="40"><?php echo $contents; ?></textarea><BR>
mail<BR>
<?php
if ($mailaddr === $oldmail){
    print "<P> mailaddr has already registered.\n";
}
?>
<input type="text" name="mailaddr" value="<?php echo $mailaddr; ?>" size="10" ><BR>
<input type="submit"  value="Confirm">
</form>
<P>
<form action="confirm.php" method = "POST">
<INPUT type="HIDDEN" name="cancel" value="1">
<input type="submit"  value="Cancel">
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

$uploadTo = "/home/tmp/$imgfile";
$errmovefile = "";
if(($sts=move_uploaded_file($tmp_path, $uploadTo))==TRUE){
   $_SESSION['showfile'] = 'showfile.php?path=' . $uploadTo;
   $_SESSION['filetype'] = $filetype;
} else {
	$errmovefile = "error move file sts:" . $sts;
}


?>
<html>
<head>
<tltle>
inquiry confirmation
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>

<form action="complete.php" method="POST">
<input type="hidden" name="token3" value="<?php echo $randomval; ?>">
confirm your input below.<BR>
<table border="1">
<tr>
<th>Subject</th><td><?php echo $subject; ?></td>
</tr>
<tr>
<th>Contents</th><td><?php echo $contents; ?></td>
</tr>
<tr>
<th>mailaddr</th><td><?php echo $mailaddr; ?></td>
</tr>
<tr>
<th>file error</th><td><?php echo $errmovefile; ?></td>
</tr>
<tr>
<th>imgfile</th><td><?php echo $imgfile; ?></td>
</tr>
<tr>
<th>file stored path</th><td><?php echo $tmp_path; ?></td>
</tr>
</table>

<input type="submit"  value="Complete">
</form>
<P>
<form action="confirm.php" method = "POST">
<INPUT type="HIDDEN" name="cancel" value="1">
<input type="submit"  value="Cancel">
</form>

<P>
<A HREF="logout.php">logout</A>
</body>
</html>



