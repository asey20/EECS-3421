select s.name, s.cat, s.cost
from (select r.cid, max(r.cost) cost
	from (select c.cid, c.name, b.cat, sum(o.price) cost
		from yrb_customer c, yrb_book b, yrb_purchase p, yrb_offer o
		where c.cid = p.cid AND p.club = o.club AND b.title = p.title AND b.year = p.year AND p.title = o.title AND p.year = o.year
		group by c.cid, c.name, b.cat) r
	group by r.cid) t, 
      (select c.cid, c.name, b.cat, sum(o.price) cost
	from yrb_customer c, yrb_book b, yrb_purchase p, yrb_offer o
	where c.cid = p.cid AND p.club = o.club AND b.title = p.title AND b.year = p.year AND p.title = o.title AND p.year = o.year
	group by c.cid, c.name, b.cat) s
where t.cid = s.cid AND t.cost = s.cost
; 
