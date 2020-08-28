<?php

include 'env.php';

session_start();
$step = 0;
$stepname = getenv('SCRIPT_FILENAME') . '.step';

if (!isset($_SESSION[$stepname])) {
    $_SESSION = array();
    $_SESSION[$stepname] = $step;
} else {
    $step = $_SESSION[$stepname] ;
}

$link = pg_connect($dbconnectinfo);

if (!$link) {
    $ERRORMESS = pg_last_error($link);
    header('location: index.php?errormess=' . urlencode($ERRORMESS));
    exit();
}

if (isset($_GET['sqlprint']) ){
    $sqlprint = 1;
}

$token1 = $_POST['token1'];
$token2 = $_POST['token2'];

if( isset($_SESSION['user'])){
    $user = $_SESSION['user'];
}else {
    $user = $_POST['user'];
}
if( isset($_SESSION['password'])){
    $password = $_SESSION['password'];
}else {
    $password = $_POST['password'];
}
if( isset($_SESSION['age'])){
    $age = $_SESSION['age'];
}else {
    $age = $_POST['age'];
}



$prevtoken1 = $_SESSION['token1'];
$prevtoken2 = $_SESSION['token2'];

$errormess = "";

if ( !empty($user) ) {
        $sql = "SELECT username,password  FROM account where username='" . $user . "'" ;
        $result = pg_query($link, $sql);
        if (!$result) {
            $ERRORMESS = pg_last_error($link);
            header('location: index.php?errormess=' . urlencode($ERRORMESS));
            exit(-1);
        }
        if(pg_num_rows($result) >= 1 ){
            $errormess .= $user . " already registered.<BR>\n";
            $user = "";
        }
} else if($step > 0){
    $errormess .= "must input user.<BR>\n";
}

if (empty($password) && $step > 0){
    $errormess .= "must input password.<BR>\n";
}

if (!is_numeric($age) && $step > 0){
    $errormess .= "You can enter only numbers for age.<BR>\n";
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
            $errormess .= "Invalid token2 value.";
        }
    }
}

$randomval = sha1(uniqid(rand(), true));

if( $step == 0
|| empty($user)
|| empty($password)
|| !empty($errormess)
 ){ // input page

    $step = 1;
    $_SESSION[$stepname] = $step;
    $_SESSION['token1'] = $randomval;

?>

<html>
<head>
<tltle>
Regist new user
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

<form action="newuser.php" method="POST">
<input type="hidden" name="token1" value="<?php echo $randomval; ?>">
username<input type="text" name="user" value="<?php echo $user; ?>"><BR>
password<input type="password" name="password" value="<?php echo $password; ?>"><BR>
age<input type="text" name="age" value="<?php echo $age; ?>" >
<P>


<input type="submit"  value="Confirm">
</form>

<P>
<A HREF="logout.php">return to index.php</A>
</body>
</html>
<?php
    exit(0);
} else if($step == 1  && empty($errormess)) { // confirm
    $step = 2;
    $_SESSION[$stepname] = $step;
    $_SESSION['token2'] = $randomval;
    $_SESSION['user'] = $user;
    $_SESSION['password'] = $password;
    $_SESSION['age'] = $age;
?>
<html>
<head>
<tltle>
Regist user confirmation
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?>

<form action="newuser.php" method="POST">
<input type="hidden" name="token2" value="<?php echo $randomval; ?>">
confirm your input below.<BR>
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
<form action="newuser.php" method = "POST">
<INPUT type="HIDDEN" name="user" value="<?php echo $user; ?>" >
<INPUT type="HIDDEN" name="password" value="" >
<INPUT type="HIDDEN" name="age" value="<?php echo $age; ?>" >
<INPUT type="HIDDEN" name="stepdown" value="1">
<input type="submit"  value="return to input">
</form>

<P>
</body>
</html>
<?php
    exit(0);
} else { // complete

        $sql = "INSERT INTO account  VALUES('"
         . substr($user, 0, 50) . "','"
         . substr($password,0,200) . "',"
         . (int)$age  . ")";
        $result = pg_query($link, $sql);
        if (!$result) {
            $ERRORMESS = pg_last_error($link);
            header('location: index.php?errormess=' . urlencode($ERRORMESS));
            exit(-1);
        }
        pg_close($link);
        // セッション変数を全て解除する
        $_SESSION = array();

        // セッションを切断するにはセッションクッキーも削除する。
        // Note: セッション情報だけでなくセッションを破壊する。
        if (isset($_COOKIE[session_name()])) {
            setcookie(session_name(), '', time()-42000, '/');
        }

        // 最終的に、セッションを破壊する
        session_destroy();
?>
<html>
<head>
<tltle>
Regist user completed
</tltle>
</head>
<body>
<P> Your user account has registered. Thank you.
<P> user:<?php echo $user; ?>
<?php
if ($sqlprint == 1) {
?>
    <P> SQL[<?php echo $sql; ?>]<P>
<?php
}
?>
<P>
<A HREF="index.php?DB=1">Login</A>
</body>
</html>
<?php
    exit(0);
}
?>
