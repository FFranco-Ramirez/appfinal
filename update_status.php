<?php
require __DIR__ . '/db.php';
$input = json_decode(file_get_contents('php://input'), true);
if (!$input || !isset($input['sensor_id'], $input['status'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Campos requeridos: sensor_id, status']);
    exit;
}
$stmt = $pdo->prepare('UPDATE sensors SET status = ? WHERE id = ?');
$stmt->execute([$input['status'], $input['sensor_id']]);
echo json_encode(['ok' => true]);
?>
