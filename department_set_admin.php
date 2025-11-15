<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
require __DIR__ . '/db.php';
function respond($payload,$status=200){ http_response_code($status); echo json_encode($payload, JSON_UNESCAPED_UNICODE); exit; }
$input = json_decode(file_get_contents('php://input'), true);
if (!$input || !isset($input['department_id'], $input['requested_by'])) {
    respond(['ok'=>false, 'error'=>'Campos requeridos: department_id, requested_by'], 400);
}
$departmentId = (int)$input['department_id'];
$requestedBy = (int)$input['requested_by'];
try {
    $stmt = $pdo->prepare('SELECT id_usuario, rol, estado, department_id FROM usuarios WHERE id_usuario = ? LIMIT 1');
    $stmt->execute([$requestedBy]);
    $u = $stmt->fetch(PDO::FETCH_ASSOC);
    if (!$u || $u['rol'] !== 'ADMIN' || $u['estado'] !== 'ACTIVO' || $u['department_id'] !== null) {
        respond(['ok'=>false, 'error'=>'no_autorizado'], 403);
    }
    $stmt = $pdo->prepare('SELECT COUNT(*) FROM usuarios WHERE department_id = ? AND rol = "ADMIN" AND estado = "ACTIVO"');
    $stmt->execute([$departmentId]);
    if ((int)$stmt->fetchColumn() > 0) {
        respond(['ok'=>false, 'error'=>'departamento_ya_tiene_admin'], 400);
    }
    $stmt = $pdo->prepare('UPDATE usuarios SET department_id = ? WHERE id_usuario = ?');
    $stmt->execute([$departmentId, $requestedBy]);
    respond(['ok'=>true, 'department_id'=>$departmentId]);
} catch (Throwable $e) {
    respond(['ok'=>false, 'error'=>'db_error', 'message'=>$e->getMessage()], 500);
}
?>
