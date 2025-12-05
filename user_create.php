<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

require __DIR__ . '/db.php';

function respond($payload, $status = 200) {
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_UNICODE);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!$input || !isset($input['nombre'], $input['email'], $input['password'], $input['rol'], $input['requested_by'])) {
    respond(['ok' => false, 'error' => 'Campos requeridos: nombre, email, password, rol, requested_by (department_id solo para OPERADOR)'], 400);
}

$nombre = trim($input['nombre']);
$email = trim($input['email']);
$password = (string)$input['password'];
$rol = strtoupper(trim($input['rol']));
$requestedBy = (int)$input['requested_by'];
$departmentId = isset($input['department_id']) ? (int)$input['department_id'] : null;

if ($rol !== 'ADMIN' && $rol !== 'OPERADOR') {
    respond(['ok' => false, 'error' => 'rol_invalido'], 400);
}

try {
    $stmt = $pdo->prepare('SELECT id_usuario, rol, estado, department_id FROM usuarios WHERE id_usuario = ? LIMIT 1');
    $stmt->execute([$requestedBy]);
    $admin = $stmt->fetch(PDO::FETCH_ASSOC);
    if (!$admin || $admin['rol'] !== 'ADMIN' || $admin['estado'] !== 'ACTIVO') {
        respond(['ok' => false, 'error' => 'no_autorizado'], 403);
    }

    if ($rol === 'OPERADOR') {
        if ($departmentId === null) {
            respond(['ok' => false, 'error' => 'department_id_requerido_para_operador'], 400);
        }
        if ((int)$admin['department_id'] !== (int)$departmentId) {
            respond(['ok' => false, 'error' => 'no_autorizado'], 403);
        }
    } else {
        $departmentId = null;
    }

    $hash = password_hash($password, PASSWORD_BCRYPT);
    $stmt = $pdo->prepare('INSERT INTO usuarios (nombre, email, password_hash, rol, estado, department_id) VALUES (?, ?, ?, ?, "ACTIVO", ?)');
    $stmt->execute([$nombre, $email, $hash, $rol, $departmentId]);
    respond(['ok' => true, 'user_id' => (int)$pdo->lastInsertId()]);
} catch (Throwable $e) {
    $message = $e->getMessage();
    if (strpos($message, 'uq_usuarios_email') !== false) {
        respond(['ok' => false, 'error' => 'email_duplicado'], 409);
    }
    respond(['ok' => false, 'error' => 'db_error', 'message' => $message], 500);
}
?>
