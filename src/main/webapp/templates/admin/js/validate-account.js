// Validate Register
const btnRegister = document.querySelector(".button-register");
if(btnRegister) {
  const formRegister = document.querySelector(".form-register");

  const inputName = document.querySelector(".inner-name");
  const inputEmail = document.querySelector(".inner-email");
  const inputNameLogin = document.querySelector(".inner-name-login");
  const inputPassword = document.querySelector(".inner-password");
  const inputCheck = document.querySelector(".inner-check");

  const feedbackName = document.querySelector(".feedback-name");
  const feedbackEmail = document.querySelector(".feedback-email");
  const feedbackNameLogin = document.querySelector(".feedback-name-login");
  const feedbackPassword = document.querySelector(".feedback-password");
  const feedbackCheck = document.querySelector(".feedback-check");
  
  btnRegister.addEventListener("click", (e) => {
    e.preventDefault();

    let isValid = true;

    // Name
    const inputNameValue = inputName.value;
    const regexName = /[\d]/;
    if(inputNameValue== "") {
      inputName.style.border = "1px solid #dc3545"
      feedbackName.innerHTML = "Vui lòng nhập tên của bạn.";
      isValid = false;
    } else if(regexName.test(inputNameValue)) {
      feedbackName.innerHTML = "Vui lòng nhập tên đúng định dạng.";
      isValid = false;
    } else {
      feedbackName.innerHTML = ""
      inputName.style.border = "1px solid #dee2e6"
    }

    // Email
    const inputEmailValue = inputEmail.value;
    const regexEmail = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if(inputEmailValue== "") {
      inputEmail.style.border = "1px solid #dc3545"
      feedbackEmail.innerHTML = "Vui lòng nhập địa chỉ Email của bạn.";
      isValid = false;
    } else if (!regexEmail.test(inputEmailValue)) {
      inputEmail.style.border = "1px solid #dc3545"
      feedbackEmail.innerHTML = "Vui lòng nhập địa chỉ Email hợp lệ.";
      isValid = false;
    } else {
      inputEmail.style.border = "1px solid #dee2e6"
      feedbackEmail.innerHTML = "";
    }

    // Name login
    const inputNameLoginValue = inputNameLogin.value;
    const regexNameLogin = /^(?=.{2,}$)(?![_.-])[a-zA-Z0-9._-]+(?<![_.-])$/;
    if(inputNameLoginValue == "") {
      inputNameLogin.style.border = "1px solid #dc3545";
      feedbackNameLogin.innerHTML = "Vui lòng nhập tên đăng nhập.";
      isValid = false;
    } else if (!regexNameLogin.test(inputNameLoginValue)) {
      inputNameLogin.style.border = "1px solid #dc3545"
      feedbackNameLogin.innerHTML = "Vui lòng nhập tên đăng nhập hợp lệ.";
      isValid = false;
    } else {
      inputNameLogin.style.border = "1px solid #dee2e6"
      feedbackNameLogin.innerHTML = "";
    }

    // Password
    const inputPasswordValue = inputPassword.value;
    const regexPassword = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
    if(inputPasswordValue == "") {
      inputPassword.style.border = "1px solid #dc3545";
      feedbackPassword.innerHTML = "Vui lòng nhập mật khẩu.";
      isValid = false;
    } else if(!regexPassword.test(inputPasswordValue)) {
      inputPassword.style.border = "1px solid #dc3545";
      feedbackPassword.innerHTML = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
      isValid = false;
    } else {
      inputPassword.style.border = "1px solid #dee2e6"
      feedbackPassword.innerHTML = "";
    }

    // Check
    if(!inputCheck.checked) {
      feedbackCheck.innerHTML = "Bạn phải đồng ý điều khoản và điều kiện trước khi đăng ký.";
      isValid = false;
    } else {
      feedbackCheck.innerHTML = "";
    }

    if(isValid) {
      formRegister.submit();
      alert("Đăng ký thành công!");
    }
    
  })
}


// End Validate Register