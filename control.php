<?php
require __DIR__ . '/db.php';
$input = json_decode(file_get_contents('php://input'), true);
if (!$input || !isset($input['device_id'], $input['department_id'], $input['action'], $input['requested_by'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Campos requeridos: device_id, department_id, action, requested_by']);
    exit;
}
$action = in_array($input['action'], ['open', 'close']) ? $input['action'] : null;
if (!$action) {
    http_response_code(400);
    echo json_encode(['error' => 'Acción inválida']);
    exit;
}
$stmt = $pdo->prepare('INSERT INTO barrier_commands (device_id, department_id, action, status, requested_by_user_id, created_at) VALUES (?, ?, ?, "queued", ?, NOW())');
$stmt->execute([$input['device_id'], $input['department_id'], $action, $input['requested_by']]);
echo json_encode(['ok' => true, 'command_id' => $pdo->lastInsertId()]);
?>