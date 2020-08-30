<?php

include 'env.php';
include 'clearSessionTokens.php';

session_start();
$step = 0;
$stepname = getenv('SCRIPT_FILENAME') . '.step';

if(isset($_SESSION['sqlprint'])) {
    $sqlprint = 1;
}

if (isset($_POST['cancel'])) {
    unset($_SESSION[$stepname]);
    clearSessionTokens();
    unset($_SESSION['oldpassword']);
    unset($_SESSION['oldage']);
    header('location: mypage.php');
    exit(-1);
}

if (!isset($_SESSION[$stepname])){
    clearSessionTokens();
    $_SESSION[$stepname] = $step;
} else {
    $step = $_SESSION[$stepname] ;
}


$link = pg_connect($dbconnectinfo);

if (!$link) {
    $ERRORMESS = pg_last_error($link);
    header('location: index.php?errormess=' . urlencode($ERRORMESS));
    exit(-1);
}



if( isset($_SESSION['user'])){
    $user = $_SESSION['user'];
}else {
    $ERRORMESS = "ERROR SESSION user no exist.";
    header('location: index.php?errormess=' . urlencode($ERRORMESS));
    exit(-1);
}

if ($step == 0) {
    $sql = "SELECT username,password,age  FROM account where username='" . $user . "'" ;
    $result = pg_query($link, $sql);
    if (!$result) {
      $ERRORMESS = pg_last_error($link);
      header('location: index.php?errormess=' . urlencode($ERRORMESS));
      exit(-1);
    }
    for ($i = 0 ; $i < pg_num_rows($result) ; $i++){
        $rows = pg_fetch_array($result, NULL, PGSQL_ASSOC);
        $oldpassword = $rows['password'];
        $oldage = $rows['age'];
    }
    if (!empty($oldpassword) ){
        $_SESSION['oldpassword'] = $oldpassword;
        $_SESSION['oldage'] = $oldage;
    } else {
        unset($_SESSION['oldpassword']);
    }

}

if(!isset($_SESSION['oldpassword'])){
    $ERRORMESS = "ERROR SESSION oldpassword has no exist.";
    header('location: index.php?errormess=' . urlencode($ERRORMESS));
    exit(-1);
} else {
    $oldpassword = $_SESSION['oldpassword'];
    $oldage = $_SESSION['oldage'];
}

$token1 = $_POST['token1'];
$token2 = $_POST['token2'];


if($step == 0){
    $age = $oldage;
}else if ($step == 1) {
    $password = $_POST['password'];
    $age = $_POST['age'];
}

if( $step > 1 && isset($_SESSION['password'])){
    $password = $_SESSION['password'];
}


if( $step > 1 && isset($_SESSION['age'])){
    $age = $_SESSION['age'];
}

$prevtoken1 = $_SESSION['token1'];
$prevtoken2 = $_SESSION['token2'];

$errormess = "";



if (empty($age) && $step > 0){
    $errormess .= "You can enter only numbers greater than 0 for age.<BR>\n";
}

if ( $step == 1 ) {
    if (empty($prevtoken1) || $prevtoken1 !== $token1 ){
        $step = 0;
        $_SESSION[$stepname] = $step;
        $errormess .= "Invalid token1 value.\n";
    }
} else if ( $step == 2 ) {
    if (empty($prevtoken2) || $prevtoken2 !== $token2 ){
        $step = 1;
        $_SESSION[$stepname] = $step;
        if (!empty($_POST['stepdown']) ){
            $errormess .= "\n";
        } else {
            $errormess .= "Invalid token2 value.\n";
        }
    }
}

$randomval = sha1(uniqid(rand(), true));

if( $step == 0
|| empty($user)
|| !empty($errormess) )
{ // input page

    $step = 1;
    $_SESSION[$stepname] = $step;
    $_SESSION['token1'] = $randomval;

?>

<html>
<head>
<tltle>
modify user
</tltle>
</head>
<body>

<P>
<?php
    if (!empty($errormess) ) {
?>
<font color="red" ><?php echo $errormess; ?></font><P>
<?php
    }
?>

<form action="moduser.php" method="POST">
<input type="hidden" name="token1" value="<?php echo $randomval; ?>">
username: <?php echo $user; ?><BR>
password<input type="password" name="password" value=""><BR>
age<input type="text" name="age" value="<?php echo $age; ?>" >
<BR>


<input type="submit"  value="Confirm">
</form><BR>
<form action="moduser.php" method = "POST">
<INPUT type="HIDDEN" name="cancel" value="1">
<input type="submit"  value="Cancel">
</form>
<P>
<A HREF="logout.php">logout</A>
</body>
</html>
<?php
    exit(0);
} else if($step == 1 && empty($errormess)){ // confirm
    $step = 2;
    $_SESSION[$stepname] = $step;
    $_SESSION['token2'] = $randomval;
    if (empty($password) ){
        $password = $oldpassword;
    }
    $_SESSION['password'] = $password;
    $_SESSION['age'] = $age;
?>
<html>
<head>
<tltle>
mod user confirmation
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>

<form action="moduser.php" method="POST">
<input type="hidden" name="token2" value="<?php echo $randomval; ?>">
confirm your input below, then press complete button. <BR>
<table border="1">
<tr>
<th>user</th><td><?php echo $user; ?></td>
</tr>
<tr>
<th>password</th><td>XXXXXXXXXXXXXXX</td>
</tr>
<tr>
<th>age</th><td><?php echo $age; ?></td>
</tr>
</table>

<input type="submit"  value="Complete">
</form>
<form action="moduser.php" method = "POST">
<INPUT type="HIDDEN" name="user" value="<?php echo $user; ?>" >
<INPUT type="HIDDEN" name="password" value="" >
<INPUT type="HIDDEN" name="age" value="<?php echo $age; ?>" >
<INPUT type="HIDDEN" name="stepdown" value="1">
<input type="submit"  value="return to input">
</form>
<form action="moduser.php" method = "POST">
<INPUT type="HIDDEN" name="cancel" value="1">
<input type="submit"  value="Cancel">
</form>

<P>
</body>
</html>
<?php
    exit(0);
} else { // complete

        $sql = "UPDATE account  SET password = '"
         . substr($password,0,200) . "', age = "
         . $age  . " WHERE username = '" . $user . "'";
        $result = pg_query($link, $sql);
        if (!$result) {
            $ERRORMESS = pg_last_error($link);
            header('location: index.php?errormess=' . urlencode($ERRORMESS));
            exit(-1);
        }
        pg_close($link);
        clearSessionTokens();
        unset($_SESSION[$stepname]);
        $step = 0;

?>
<html>
<head>
<tltle>
user mod completed
</tltle>
</head>
<body>
<P> your user account has modified.
<P> user:<?php echo $user; ?>
<?php
if ($sqlprint == 1) {
?>
    <P> SQL[<?php echo $sql; ?>]<P>
<?php
}
?>
<P>
<form action="moduser.php" method = "POST">
<INPUT type="HIDDEN" name="cancel" value="1">
<input type="submit"  value="Return to MYPAGE">
</form>
</body>
</html>
<?php
    exit(0);
}
?>
