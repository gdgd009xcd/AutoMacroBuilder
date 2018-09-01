<?php 

if(isset($_POST['user'])){
    $user = $_POST['user'];
}else{
    $user ="";
}

if(isset($_POST['pass'])){
    $pass = $_POST['pass'];
}else{
    $pass = "";
}

session_start();

if ( $user === "daike" && $pass === "test1234"){
	$_SESSION['user'] = $user;
}elseif( isset($_SESSION['user'])){
	//already logged in..
	$user = $_SESSION['user'];
}else{
	header('location: index.php');
	exit();
}

if(isset($_GET['token1'])){
    if ( isset($_SESSION['token1'])){
        if($_GET['token1'] === $_SESSION['token1']){
            header('location: inquiry.php?token1=' . $_GET['token1']);
            exit();
        }
    }
}

$randomval = sha1(uniqid(rand(), true));
$htoken = sha1(uniqid(rand(), true));
$_SESSION['token1'] = $randomval;
$_SESSION['htoken'] = $htoken;




?>
<html>
<head>
<tltle>
MYPAGE
</tltle>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
 <script>
  $(document).ready(function() {
    /**
     * poppボタンクリック
     */
    $('#popup').click(function() {
      
      var data = {'request' : 1};

      
      $.ajax({
        type: "POST",
        url: "popup.php",
        headers: {
            'X-SPECIAL': '<?php echo $htoken; ?>',
        },

        data: data,
      }).done(function(data, dataType) {
        var w = window.open();
        $(w.document.body).html(data);
      }).fail(function(XMLHttpRequest, textStatus, errorThrown) {
        

        
        alert('Error : ' + errorThrown);
      });

      
      return false;
    });

    // JSON
    $('#json').click(function() {
      
      var data = {'request' : 1, 'X-SPECIAL': '<?php echo $htoken; ?>'};

      
      $.ajax({
        type: "POST",
        url: "json.php",
        data:JSON.stringify(data),
        contentType: 'application/json',
        dataType: "json"
      }).done(function(data, dataType) {
        alert(JSON.stringify(data));
      }).fail(function(XMLHttpRequest, textStatus, errorThrown) {
        

        
        alert('Error : ' + errorThrown);
      });

      
      return false;
    });
  });
  </script>
</head>
<body>

<P> welcome user:<?php echo $user; ?>

<form action="inquiry.php" method="GET">
<input type="hidden" name="token1" value="<?php echo $randomval; ?>">
<input type="submit"  value="inquiry:お問い合わせ">
</form>

<P>
<A HREF="mypage.php?token1=<?php echo $randomval; ?>">ロケーションでお問い合わせに遷移</A>
<P>
<input id="popup" value="popup" type="submit" /></p>
<input id="json" value="json" type="submit" /></p>
<A HREF="logout.php">logout</A>
</body>
</html>



