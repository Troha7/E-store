function deleteById(url) {
    if (confirm("Do you want to delete this item?")) {
        fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) {
                    location.reload();
                }
            })
    }
}