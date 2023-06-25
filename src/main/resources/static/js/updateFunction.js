function updateById(url) {
    const uri = url;
    const id = url.match(/\d+/);
    const formData = new FormData(document.getElementById(id));
    const data = Object.fromEntries(formData.entries());
    const jsonData = JSON.stringify(data); // Сериализация данных формы в JSON

    console.log(jsonData);

    if (confirm("Do you want to update this item?")) {
        fetch(uri, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: jsonData // Отправка данных в формате JSON
        }).then(response => {
            if (response.ok) {
                location.reload();
            }
        });
    }
}