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


$sql = "SELECT *  FROM uploadlist where username ='" . $user . "'" ;
$result = pg_query($link, $sql);
if (!$result) {
  $ERRORMESS = pg_last_error($link);
  header('location: index.php?errormess=' . urlencode($ERRORMESS));
  exit(-1);
}
?>

<html>
<head>
<tltle>
your registered inquiry list
</tltle>
</head>
<body>

<P> user:<?php echo $user; ?><P>
<?php
if ($sqlprint == 1) {
?>
    <P> SQL[<?php echo $sql; ?>]<P>
<?php
}
?>
<P> total inquiry list: <?php echo pg_numrows($result); ?> <P>


<table border="1">
<?php
$titleprint = 0;
for ($i = 0 ; $i < pg_num_rows($result) ; $i++){
    $rows = pg_fetch_array($result, NULL, PGSQL_ASSOC);
    $keys = array_keys($rows);
    if ($titleprint == 0) {
        echo '<TR>';
        foreach($keys as $k) {
?>
            <th><?php echo $k;?></th>
<?php
        }
        echo "</TR>\n";
        $titleprint++;
    }
    echo '<TR>';
    foreach($rows as $k => $v) {
?>
        <td><?php echo $v?></td>
<?php
    }
    echo "</TR>\n";
?>
<?php
}
?>
</table>

<P>
<form action="inquirylist.php" method = "POST">
<INPUT type="HIDDEN" name="cancel" value="1">
<input type="submit"  value="Return to MYPAGE">
</form>

<P>
</body>
</html>

<?php
exit(0);
?>
