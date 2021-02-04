auth.onAuthStateChanged(user => {
    if(user) {
        console.log('Logged in now');
        
       window.location = '/complaints'; //After successful login, user will be redirected to home.html
    }else{
        console.log('Logged OUTTTT');    
    }
  });

const login_form=document.querySelector('#login_form');
    
login_form.addEventListener('submit', (e)=>{
    e.preventDefault();

    const email=login_form['username'].value;
    const password=login_form['pass'].value;

    var mailformat = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
    if(!email.match(mailformat))
    {
        alert("You have entered an invalid email address!");
        login_form.username.focus();
        return ;
    }

    if(password.length<6){
        alert("Password: Min. 6 chars");
        login_form.username.focus();
        return;
    }

    auth.signInWithEmailAndPassword(email, password).then(cred=>{
        console.log(cred.user);

        
    })

    
} )
