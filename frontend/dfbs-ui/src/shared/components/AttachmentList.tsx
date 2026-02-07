import { Space, Typography } from 'antd';

interface AttachmentListProps {
  fileUrls?: string | string[] | null;
}

function parseUrls(fileUrls: string | string[] | null | undefined): string[] {
  if (!fileUrls) return [];
  if (Array.isArray(fileUrls)) return fileUrls.filter(Boolean);
  try {
    const parsed = JSON.parse(fileUrls) as string | string[];
    return Array.isArray(parsed) ? parsed : [parsed].filter(Boolean);
  } catch {
    return String(fileUrls).split(/[,;]/).map((s) => s.trim()).filter(Boolean);
  }
}

export function AttachmentList({ fileUrls }: AttachmentListProps) {
  const urls = parseUrls(fileUrls);
  if (urls.length === 0) return <Typography.Text type="secondary">无附件</Typography.Text>;
  return (
    <Space direction="vertical" size={4}>
      {urls.map((url, i) => (
        <a key={i} href={url} target="_blank" rel="noopener noreferrer">
          {url.length > 50 ? url.slice(0, 50) + '…' : url}
        </a>
      ))}
    </Space>
  );
}
