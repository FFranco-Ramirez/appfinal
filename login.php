<?php
require __DIR__ . '/db.php';

function respond($payload, $status = 200) {
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!$input || !isset($input['email'], $input['password'])) {
    respond(['ok' => false, 'error' => 'Campos requeridos: email, password'], 400);
}

$email = trim($input['email']);
$password = (string)$input['password'];

try {
    $stmt = $pdo->prepare('SELECT id_usuario, nombre, email, password_hash, rol, estado, department_id FROM usuarios WHERE email = ? LIMIT 1');
    $stmt->execute([$email]);
    $u = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$u) {
        respond(['ok' => false, 'error' => 'usuario_no_encontrado'], 401);
    }
    if ($u['estado'] !== 'ACTIVO') {
        respond(['ok' => false, 'error' => 'usuario_inactivo'], 403);
    }
    if (!password_verify($password, $u['password_hash'])) {
        respond(['ok' => false, 'error' => 'credenciales_invalidas'], 401);
    }

    respond([
        'ok' => true,
        'user_id' => (int)$u['id_usuario'],
        'department_id' => (int)$u['department_id'],
        'rol' => $u['rol'],
        'nombre' => $u['nombre']
    ]);
} catch (Throwable $e) {
    respond(['ok' => false, 'error' => 'db_error', 'message' => $e->getMessage()], 500);
}
?>
