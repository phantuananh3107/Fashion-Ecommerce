 document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('.needs-validation');
    
    form.addEventListener('submit', function (event) {
        
        event.stopPropagation();
        
        const username = document.getElementById('yourUsername').value.trim();
        const password = document.getElementById('yourPassword').value;
        
        // Username validation
        if (username === '') {
            alert('Vui lòng nhập tên đăng nhập!');
            form.classList.add('was-validated');
            return;
        }

         if (password === '') {
            alert('Vui lòng nhập mật khẩu!');
            form.classList.add('was-validated');
            return;
        }
        
      
        
       
    });
});
