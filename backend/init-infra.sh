#!/bin/bash

# Script de inicialização do MinIO
# Cria buckets necessários para armazenar vídeos

set -e

echo "⏳ Aguardando MinIO ficar online..."
until curl -s http://localhost:9000/minio/health/live; do
  sleep 1
done
echo "✅ MinIO online!"

echo ""
echo "🪣 Configurando buckets do MinIO..."

# Configurar credenciais do MinIO
export MC_HOST_minio=http://minioadmin:minioadmin123@localhost:9000

# Criar bucket warbox-videos
if ! mc ls minio/warbox-videos > /dev/null 2>&1; then
  echo "Creating bucket warbox-videos..."
  mc mb minio/warbox-videos
  # Política pública para acesso de URLs assinadas
  mc policy set public minio/warbox-videos
  echo "✅ Bucket warbox-videos criado"
else
  echo "✅ Bucket warbox-videos já existe"
fi

echo ""
echo "✨ Setup concluído! MinIO pronto."
echo ""
echo "📊 Acessar painel:"
echo "  MinIO:      http://localhost:9001 (minioadmin / minioadmin123)"
