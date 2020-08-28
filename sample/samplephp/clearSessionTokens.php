<?php

function clearSessionTokens() {
    $tokens = array('token', 'token1', 'token2', 'token3');
    foreach($tokens as $token) {
        if( isset($_SESSION[$token])) {
            unset($_SESSION[$token]);
        }
    }
}



?>