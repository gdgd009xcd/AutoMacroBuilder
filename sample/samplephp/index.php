<?php

$DB = $_GET['DB'];


?>

<html>
<head>
<title>
login page
</title>
</head>
<body>

<form action="mypage.php" method="POST">
user:<input type="text" name="user" value=""><BR>
pass:<input type="password" name="pass" value=""><BR>
<?php

if( !empty($DB) ){
print "<input type=hidden name=\"DB\" value=\"1\"><BR>";
}
?>
<input type="submit" value="ログイン">
</form>
</body>
</html>
