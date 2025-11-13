<?php
require __DIR__ . '/db.php';
$input = json_decode(file_get_contents('php://input'), true);
if (!$input || !isset($input['uid'], $input['department_id'], $input['user_id'], $input['status'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Campos requeridos: uid, department_id, user_id, status']);
    exit;
}
$stmt = $pdo->prepare('INSERT INTO sensors (uid, department_id, user_id, status, type) VALUES (?, ?, ?, ?, ?)');
$stmt->execute([
    $input['uid'],
    $input['department_id'],
    $input['user_id'],
    $input['status'],
    $input['type'] ?? 'rfid',
]);
echo json_encode(['ok' => true, 'sensor_id' => $pdo->lastInsertId()]);
?>