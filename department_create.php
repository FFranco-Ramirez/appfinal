<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
require __DIR__ . '/db.php';
function respond($payload,$status=200){ http_response_code($status); echo json_encode($payload, JSON_UNESCAPED_UNICODE); exit; }
$input = json_decode(file_get_contents('php://input'), true);
if (!$input || !isset($input['nombre'], $input['requested_by'])) {
    respond(['ok'=>false, 'error'=>'Campos requeridos: nombre, requested_by'], 400);
}
$nombre = trim($input['nombre']);
$requestedBy = (int)$input['requested_by'];
try {
    $stmt = $pdo->prepare('SELECT id_usuario, rol, estado, department_id FROM usuarios WHERE id_usuario = ? LIMIT 1');
    $stmt->execute([$requestedBy]);
    $u = $stmt->fetch(PDO::FETCH_ASSOC);
    if (!$u || $u['rol'] !== 'ADMIN' || $u['estado'] !== 'ACTIVO' || $u['department_id'] !== null) {
        respond(['ok'=>false, 'error'=>'no_autorizado'], 403);
    }
    $stmt = $pdo->prepare('INSERT INTO departamentos (nombre) VALUES (?)');
    $stmt->execute([$nombre]);
    $depId = (int)$pdo->lastInsertId();
    $stmt = $pdo->prepare('UPDATE usuarios SET department_id = ? WHERE id_usuario = ?');
    $stmt->execute([$depId, $requestedBy]);
    respond(['ok'=>true, 'department_id'=>$depId]);
} catch (Throwable $e) {
    respond(['ok'=>false, 'error'=>'db_error', 'message'=>$e->getMessage()], 500);
}